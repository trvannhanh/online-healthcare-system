import { useEffect, useState, useRef, useCallback } from "react";
import { ref, onValue, off } from "firebase/database";
import { db } from "../configs/firebase";
import { Alert, Button, Card, Form, Spinner, Modal } from "react-bootstrap";
import { useMyUser } from "../configs/MyContexts";
import { useParams, useNavigate } from "react-router-dom";
import axios from "axios";
import Apis, { authApis, endpoints } from "../configs/Apis";
import cookie from 'react-cookies';

// Hàm lấy biểu tượng dựa trên loại file
const getFileIcon = (fileName) => {
  const extension = fileName.split('.').pop().toLowerCase();
  switch (extension) {
    case 'pdf':
      return '📄';
    case 'xlsx':
    case 'xls':
      return '📊';
    case 'doc':
    case 'docx':
      return '📜';
    case 'txt':
      return '📝';
    case 'zip':
      return '📦';
    default:
      return '📎';
  }
};

const ChatRoom = () => {
  const { otherUserId } = useParams();
  const { user } = useMyUser() || {};
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState("");
  const [file, setFile] = useState(null);
  const [image, setImage] = useState(null);
  const [loading, setLoading] = useState(true);
  const [fileUploading, setFileUploading] = useState(false);
  const [error, setError] = useState(null);
  const [otherUser, setOtherUser] = useState({ name: "Người dùng không xác định", avatar: null });
  const [showVideoCall, setShowVideoCall] = useState(false);
  const [jitsiApi, setJitsiApi] = useState(null);
  const [jitsiScriptLoaded, setJitsiScriptLoaded] = useState(false);
  const messagesEndRef = useRef(null);
  const fileInputRef = useRef(null);
  const imageInputRef = useRef(null);
  const jitsiContainerRef = useRef(null);
  const navigate = useNavigate();

  const scrollToBottom = useCallback(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, []);

  // Kiểm tra script Jitsi đã tải chưa
  useEffect(() => {
    const checkJitsiScript = () => {
      console.log("Kiểm tra Jitsi Meet script...");
      if (window.JitsiMeetExternalAPI) {
        console.log("Jitsi Meet script đã tải thành công!");
        setJitsiScriptLoaded(true);
      } else {
        console.log("Đang chờ Jitsi Meet script tải... (Kiểm tra mạng hoặc domain)");
        setTimeout(checkJitsiScript, 500); // Kiểm tra lại sau 500ms
      }
    };

    checkJitsiScript();

    // Thêm sự kiện để kiểm tra lỗi tải script
    const handleScriptError = () => {
      console.error("Lỗi khi tải script Jitsi Meet!");
      setError("Không thể tải Jitsi Meet script. Vui lòng kiểm tra kết nối hoặc domain.");
    };
    window.addEventListener('error', handleScriptError);

    return () => {
      window.removeEventListener('error', handleScriptError);
    };
  }, []);

  // Gọi initializeJitsi khi modal mở và script đã tải
  useEffect(() => {
    if (showVideoCall && jitsiScriptLoaded && !jitsiApi) {
      console.log("Modal đã mở, kiểm tra jitsiContainerRef...");
      if (!jitsiContainerRef.current) {
        console.log("jitsiContainerRef.current chưa sẵn sàng, chờ DOM render...");
        // Chờ DOM render xong
        const timer = setTimeout(() => {
          if (jitsiContainerRef.current) {
            console.log("jitsiContainerRef.current đã sẵn sàng, khởi tạo Jitsi...");
            initializeJitsi();
          } else {
            console.error("jitsiContainerRef.current vẫn là null sau khi chờ!");
            setError("Không thể tìm thấy container cho video call. Vui lòng thử lại.");
          }
        }, 100); // Chờ 100ms để DOM render
        return () => clearTimeout(timer);
      } else {
        console.log("jitsiContainerRef.current đã sẵn sàng, khởi tạo Jitsi ngay...");
        initializeJitsi();
      }
    } else if (showVideoCall && !jitsiScriptLoaded) {
      console.log("Script Jitsi chưa tải xong!");
      setError("Jitsi Meet script chưa tải xong. Vui lòng chờ hoặc làm mới trang.");
    }
  }, [showVideoCall, jitsiScriptLoaded, jitsiApi]);

  // Lấy thông tin người dùng khác
  useEffect(() => {
    const fetchOtherUser = async () => {
      if (!otherUserId || !user) {
        setError("Không tìm thấy thông tin người dùng hoặc ID đối phương.");
        return;
      }
      try {
        let userData;
        if (user.role === "PATIENT") {
          const res = await authApis().get(`${endpoints["doctors"]}/${otherUserId}`);
          userData = res.data.user;
        } else {
          const res = await authApis().get(`${endpoints["patients"]}/${otherUserId}`);
          userData = res.data.user;
        }
        setOtherUser({
          name: `${userData.firstName || ""} ${userData.lastName || ""}`.trim() || otherUserId,
          avatar: userData.avatar || (user.role === "PATIENT" ? "/images/doctor-placeholder.jpg" : "/images/patient-placeholder.jpg"),
        });
      } catch (error) {
        console.error("Lỗi khi lấy thông tin người dùng khác:", error);
        if (error.response?.status === 401) {
          navigate("/login");
        }
        setError("Không thể tải thông tin người dùng. Vui lòng thử lại.");
      }
    };
    fetchOtherUser();
  }, [otherUserId, user, navigate]);

  // Lắng nghe tin nhắn từ Firebase Realtime Database
  useEffect(() => {
    if (!user?.id || !otherUserId) return;

    const chatRoomId = user.id < otherUserId ? `${user.id}_${otherUserId}` : `${otherUserId}_${user.id}`;
    const messagesRef = ref(db, `chat_rooms/${chatRoomId}/messages`);

    setLoading(true);
    const unsubscribe = onValue(messagesRef, (snapshot) => {
      const messagesData = [];
      snapshot.forEach((childSnapshot) => {
        const message = childSnapshot.val();
        message.id = childSnapshot.key;
        messagesData.push(message);
      });
      setMessages(messagesData);
      setLoading(false);
    }, (error) => {
      setError("Không thể tải tin nhắn: " + error.message);
      setLoading(false);
    });

    return () => off(messagesRef, "value", unsubscribe);
  }, [user, otherUserId]);

  // Tự động cuộn xuống tin nhắn mới nhất
  useEffect(() => {
    scrollToBottom();
  }, [messages, scrollToBottom]);

  // Xử lý chọn file
  const handleFileChange = (e) => {
    const selectedFile = e.target.files[0];
    if (selectedFile) {
      if (selectedFile.size > 5 * 1024 * 1024) {
        setError("Kích thước tệp không được vượt quá 5MB.");
        return;
      }
      setFile(selectedFile);
      setImage(null);
    }
  };

  // Xử lý chọn ảnh
  const handleImageChange = (e) => {
    const selectedImage = e.target.files[0];
    if (selectedImage) {
      if (selectedImage.size > 5 * 1024 * 1024) {
        setError("Kích thước ảnh không được vượt quá 5MB.");
        return;
      }
      setImage(selectedImage);
      setFile(null);
    }
  };

  // Gửi tin nhắn
  const onSend = useCallback(async () => {
    if (!user?.id || !otherUserId) return;
    if (!newMessage.trim() && !file && !image) return;

    try {
      setLoading(true);
      setFileUploading(true);

      const formData = new FormData();
      if (newMessage.trim()) formData.append("text", newMessage.trim());
      if (file) formData.append("file", file);
      if (image) formData.append("image", image);
      formData.append("otherUserId", otherUserId);

      const token = cookie.load('token');
      const response = await Apis.post(`${endpoints.chat}/send`, formData, {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "multipart/form-data",
        },
      });

      setNewMessage("");
      setFile(null);
      setImage(null);
      if (fileInputRef.current) fileInputRef.current.value = "";
      if (imageInputRef.current) imageInputRef.current.value = "";
    } catch (err) {
      console.error("Lỗi khi gửi tin nhắn:", err);
      if (err.response?.status === 401) {
        navigate("/login");
      }
      setError(`Không thể gửi tin nhắn: ${err.message}`);
    } finally {
      setLoading(false);
      setFileUploading(false);
    }
  }, [user, otherUserId, newMessage, file, image, navigate]);

  // Xóa tin nhắn
  const onDelete = useCallback(async (messageId) => {
    try {
      await axios.delete(`${endpoints.chat}/messages/${messageId}`, {
        params: { otherUserId },
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
      });
    } catch (err) {
      console.error("Lỗi khi xóa tin nhắn:", err);
      if (err.response?.status === 401) {
        navigate("/login");
      }
      setError(`Không thể xóa tin nhắn: ${err.message}`);
    }
  }, [otherUserId, navigate]);

  const triggerFileInput = () => {
    fileInputRef.current?.click();
  };

  const triggerImageInput = () => {
    imageInputRef.current?.click();
  };

  // Xử lý mở video call
  const handleVideoCall = () => {
    console.log("handleVideoCall được gọi, showVideoCall:", showVideoCall, "jitsiScriptLoaded:", jitsiScriptLoaded);
    setShowVideoCall(true);
    if (!jitsiScriptLoaded) {
      console.log("Script Jitsi chưa tải xong!");
      setError("Jitsi Meet script chưa tải xong. Vui lòng chờ hoặc làm mới trang.");
    }
  };

  const handleCloseVideoCall = () => {
    console.log("Đóng modal video call...");
    if (jitsiApi) {
      console.log("Dispose Jitsi API...");
      jitsiApi.dispose();
    }
    setShowVideoCall(false);
    setJitsiApi(null);
  };

  // Khởi tạo Jitsi Meet API
  const initializeJitsi = () => {
    console.log("Bắt đầu khởi tạo Jitsi Meet...");
    console.log("jitsiContainerRef.current:", jitsiContainerRef.current);
    console.log("jitsiApi:", jitsiApi);

    if (!jitsiContainerRef.current) {
      console.error("jitsiContainerRef.current là null, kiểm tra DOM!");
      setError("Không thể tìm thấy container cho video call.");
      return;
    }
    if (jitsiApi) {
      console.log("Jitsi API đã tồn tại, không khởi tạo lại.");
      return;
    }

    const roomName = user?.id && otherUserId ? `chat_${user.id}_${otherUserId}` : 'default_room';
    const options = {
      roomName: roomName,
      width: '100%',
      height: 400,
      parentNode: jitsiContainerRef.current,
      userInfo: {
        displayName: user.firstName || user.username || "Người dùng",
      },
      configOverwrite: {
        startWithAudioMuted: true,
        disableModeratorIndicator: true,
      },
      interfaceConfigOverwrite: {
        SHOW_JITSI_WATERMARK: false,
        SHOW_WATERMARK_FOR_GUESTS: false,
      },
      onLoad: () => {
        console.log('Jitsi Meet API đã tải thành công');
      },
    };

    try {
      if (!window.JitsiMeetExternalAPI) {
        console.error("JitsiMeetExternalAPI không tồn tại, kiểm tra script!");
        setError("Không thể tải Jitsi Meet API. Vui lòng kiểm tra kết nối hoặc script.");
        return;
      }
      const api = new window.JitsiMeetExternalAPI('meet.jit.si', options);
      api.addEventListener('videoConferenceJoined', () => {
        console.log("Đã tham gia cuộc gọi video!");
      });
      api.addEventListener('videoConferenceLeft', handleCloseVideoCall);
      api.addEventListener('errorOccurred', (error) => {
        console.error("Lỗi từ Jitsi Meet:", error);
        setError("Đã xảy ra lỗi trong cuộc gọi video: " + error.message);
      });
      setJitsiApi(api);
      console.log("Jitsi API khởi tạo thành công:", api);
    } catch (error) {
      console.error("Lỗi khi khởi tạo Jitsi API:", error);
      setError("Không thể khởi tạo cuộc gọi video: " + error.message);
    }
  };

  if (!user) {
    return (
      <div className="container py-5">
        <Alert variant="warning">
          Vui lòng đăng nhập để sử dụng tính năng chat!{" "}
          <a href="/login" className="ms-2">Đăng nhập</a>
        </Alert>
      </div>
    );
  }

  return (
    <div className="container py-5">
      <Card className="shadow border-0" style={{ maxWidth: "700px", margin: "0 auto", borderRadius: "15px" }}>
        <Card.Header className="bg-primary text-white d-flex align-items-center justify-content-between p-3">
          <div className="d-flex align-items-center">
            <img
              src={otherUser.avatar || "/images/placeholder.jpg"}
              alt={otherUser.name}
              style={{ width: "40px", height: "40px", borderRadius: "50%", objectFit: "cover", marginRight: "15px" }}
            />
            <h4 className="mb-0">{otherUser.name}</h4>
          </div>
          <Button
            variant="outline-light"
            size="sm"
            onClick={handleVideoCall}
            aria-label="Bắt đầu gọi video"
            style={{ borderRadius: "20px" }}
          >
            📹 Gọi video
          </Button>
        </Card.Header>
        <Card.Body className="p-0">
          {error && (
            <Alert variant="danger" dismissible onClose={() => setError(null)} className="m-3">
              {error}
            </Alert>
          )}
          {loading && (
            <div className="text-center py-3">
              <Spinner animation="border" variant="primary" />
            </div>
          )}
          {!loading && (
            <div
              className="chat-messages p-4"
              style={{ height: "450px", overflowY: "auto", backgroundColor: "#f8f9fa" }}
            >
              {messages.map((msg) => {
                const isCurrentUser = msg.senderId === user.id.toString();
                return (
                  <div
                    key={msg.id}
                    className={`d-flex mb-4 ${isCurrentUser ? "justify-content-end" : "justify-content-start"}`}
                  >
                    {!isCurrentUser && (
                      <img
                        src={msg.senderAvatar || "/images/placeholder.jpg"}
                        alt={msg.senderName}
                        style={{ width: "35px", height: "35px", borderRadius: "50%", objectFit: "cover", marginRight: "15px", alignSelf: "flex-end" }}
                      />
                    )}
                    <div
                      className={`p-3 ${isCurrentUser ? "bg-primary text-white" : "bg-white text-dark"} position-relative`}
                      style={{
                        borderRadius: "12px",
                        maxWidth: "75%",
                        boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
                        border: isCurrentUser ? "none" : "1px solid #e0e0e0",
                      }}
                    >
                      <small className="d-block" style={{ opacity: "0.7", fontSize: "0.85em", marginBottom: "5px" }}>
                        {msg.senderName}
                      </small>
                      {msg.type === "image" && (
                        <a href={msg.fileUrl} target="_blank" rel="noopener noreferrer">
                          <img
                            src={msg.fileUrl}
                            alt="Ảnh đã gửi"
                            className="img-fluid rounded mb-2"
                            style={{ maxHeight: "200px", maxWidth: "100%", transition: "transform 0.2s" }}
                            onMouseEnter={(e) => (e.currentTarget.style.transform = "scale(1.02)")}
                            onMouseLeave={(e) => (e.currentTarget.style.transform = "scale(1)")}
                          />
                        </a>
                      )}
                      {msg.type === "file" && (
                        <a
                          href={msg.fileUrl}
                          download={msg.fileName}
                          className="d-flex align-items-center text-decoration-none mb-2"
                          style={{
                            color: isCurrentUser ? "#ffffff" : "#007bff",
                            padding: "8px",
                            borderRadius: "8px",
                            backgroundColor: isCurrentUser ? "rgba(255,255,255,0.1)" : "rgba(0,123,255,0.05)",
                            transition: "background-color 0.2s",
                          }}
                          onMouseEnter={(e) => (e.currentTarget.style.backgroundColor = isCurrentUser ? "rgba(255,255,255,0.2)" : "rgba(0,123,255,0.1)")}
                          onMouseLeave={(e) => (e.currentTarget.style.backgroundColor = isCurrentUser ? "rgba(255,255,255,0.1)" : "rgba(0,123,255,0.05)")}
                        >
                          <span className="me-2" style={{ fontSize: "1.2em" }}>
                            {getFileIcon(msg.fileName)}
                          </span>
                          <span style={{ fontSize: "0.95em", wordBreak: "break-all" }}>{msg.fileName}</span>
                        </a>
                      )}
                      {msg.text && <p className="mb-2" style={{ lineHeight: "1.5" }}>{msg.text}</p>}
                      <small className="d-block" style={{ opacity: "0.5", fontSize: "0.75em" }}>
                        {new Date(parseInt(msg.timestamp)).toLocaleString("vi-VN", {
                          day: "2-digit",
                          month: "2-digit",
                          year: "numeric",
                          hour: "2-digit",
                          minute: "2-digit",
                        })}
                      </small>
                      {isCurrentUser && (
                        <Button
                          variant="link"
                          size="sm"
                          className="position-absolute"
                          style={{ top: "8px", right: "8px", color: "red", padding: 0 }}
                          onClick={() => onDelete(msg.id)}
                          aria-label="Xóa tin nhắn"
                        >
                          🗑️
                        </Button>
                      )}
                    </div>
                    {isCurrentUser && (
                      <img
                        src={user.avatar || "/images/placeholder.jpg"}
                        alt={user.firstName || user.username}
                        style={{ width: "35px", height: "35px", borderRadius: "50%", objectFit: "cover", marginLeft: "15px", alignSelf: "flex-end" }}
                      />
                    )}
                  </div>
                );
              })}
              <div ref={messagesEndRef} />
            </div>
          )}
        </Card.Body>
        <Card.Footer className="p-3 bg-light">
          <Form
            className="d-flex align-items-center"
            onSubmit={(e) => {
              e.preventDefault();
              onSend();
            }}
          >
            <Form.Control
              type="file"
              ref={fileInputRef}
              onChange={handleFileChange}
              accept=".pdf,.doc,.docx,.xlsx,.xls,.txt,.zip"
              style={{ display: "none" }}
              aria-label="Chọn tệp để gửi"
            />
            <Button
              variant="outline-primary"
              size="sm"
              className="me-2"
              onClick={triggerFileInput}
              disabled={loading || fileUploading || !user.id || !otherUserId}
              aria-label="Gửi tệp"
              style={{ borderRadius: "50%", width: "40px", height: "40px", display: "flex", alignItems: "center", justifyContent: "center" }}
            >
              {fileUploading && file ? (
                <Spinner animation="border" size="sm" />
              ) : (
                <svg
                  width="18"
                  height="18"
                  fill="currentColor"
                  viewBox="0 0 16 16"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path d="M8 3a.5.5 0 0 1 .5.5v3h3a.5.5 0 0 1 0 1h-3v3a.5.5 0 0 1-1 0v-3h-3a.5.5 0 0 1 0-1h3v-3A.5.5 0 0 1 8 3z"/>
                </svg>
              )}
            </Button>
            <Form.Control
              type="file"
              ref={imageInputRef}
              onChange={handleImageChange}
              accept="image/*"
              style={{ display: "none" }}
              aria-label="Chọn ảnh để gửi"
            />
            <Button
              variant="outline-primary"
              size="sm"
              className="me-2"
              onClick={triggerImageInput}
              disabled={loading || fileUploading || !user.id || !otherUserId}
              aria-label="Gửi ảnh"
              style={{ borderRadius: "50%", width: "40px", height: "40px", display: "flex", alignItems: "center", justifyContent: "center" }}
            >
              {fileUploading && image ? (
                <Spinner animation="border" size="sm" />
              ) : (
                <svg
                  width="18"
                  height="18"
                  fill="currentColor"
                  viewBox="0 0 16 16"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path d="M4 2a2 2 0 0 0-2 2v8a2 2 0 0 0 2 2h8a2 2 0 0 0 2-2V4a2 2 0 0 0-2-2H4zm0 1h8a1 1 0 0 1 1 1v8a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1zm3 7.5a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3z"/>
                </svg>
              )}
            </Button>
            <Form.Control
              type="text"
              value={newMessage}
              onChange={(e) => setNewMessage(e.target.value)}
              placeholder="Nhập tin nhắn..."
              disabled={loading || fileUploading || !user.id || !otherUserId}
              className="flex-grow-1 me-2"
              aria-label="Nhập tin nhắn"
              style={{ borderRadius: "20px", padding: "10px 15px", borderColor: "#ced4da" }}
            />
            <Button
              variant="primary"
              onClick={onSend}
              disabled={loading || fileUploading || !user.id || !otherUserId || (!newMessage.trim() && !file && !image)}
              aria-label="Gửi tin nhắn, tệp hoặc ảnh"
              style={{ borderRadius: "20px", padding: "8px 20px" }}
            >
              {loading ? <Spinner animation="border" size="sm" /> : "Gửi"}
            </Button>
          </Form>
        </Card.Footer>
      </Card>

      {/* Modal hiển thị giao diện video call */}
      <Modal
        show={showVideoCall}
        onHide={handleCloseVideoCall}
        size="lg"
        centered
        backdrop="static"
      >
        <Modal.Header closeButton>
          <Modal.Title>Gọi video với {otherUser.name}</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <div ref={jitsiContainerRef} style={{ width: "100%", height: "400px", backgroundColor: "#f0f0f0" }} />
          {error && <Alert variant="danger">{error}</Alert>}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="danger" onClick={handleCloseVideoCall}>
            Kết thúc cuộc gọi
          </Button>
        </Modal.Footer>
      </Modal>
    </div>
  );
};

export default ChatRoom;
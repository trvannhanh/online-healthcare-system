import { useEffect, useState, useRef, useCallback } from "react";
import { ref, onValue, push, set, off } from "firebase/database";
import { ref as storageRef, uploadBytes, getDownloadURL } from "firebase/storage";
import { db, storage } from "../configs/firebase";
import { Alert, Button, Card, Form, Spinner } from "react-bootstrap";
import { useMyUser } from "../configs/MyContexts";
import { useParams } from "react-router-dom";
import Apis, { authApis, endpoints } from "../configs/Apis";

const ChatRoom = () => {
  const { otherUserId } = useParams();
  const { user } = useMyUser() || {};
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState("");
  const [file, setFile] = useState(null);
  const [image, setImage] = useState(null); // Thêm state cho ảnh
  const [loading, setLoading] = useState(true);
  const [fileUploading, setFileUploading] = useState(false);
  const [error, setError] = useState(null);
  const [otherUser, setOtherUser] = useState({ name: "Người dùng không xác định", avatar: null });
  const messagesEndRef = useRef(null);
  const fileInputRef = useRef(null);
  const imageInputRef = useRef(null); // Thêm ref cho input ảnh

  const scrollToBottom = useCallback(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, []);

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
        setError("Không thể tải thông tin người dùng. Vui lòng thử lại.");
      }
    };
    fetchOtherUser();
  }, [otherUserId, user]);

  useEffect(() => {
    let unsubscribe;
    let chatRef = null;
    if (user?.id && otherUserId) {
      chatRef = ref(db, `chat_rooms/${user.role === "PATIENT" ? `${user.id}_${otherUserId}` : `${otherUserId}_${user.id}`}/messages`);
      unsubscribe = onValue(
        chatRef,
        (snapshot) => {
          const data = snapshot.val();
          if (data) {
            const messageList = Object.keys(data).map((key) => ({
              id: key,
              type: data[key].type || "text",
              text: data[key].text || "",
              fileUrl: data[key].fileUrl || null,
              fileName: data[key].fileName || null,
              createdAt: new Date(data[key].timestamp),
              senderId: data[key].senderId,
              senderName: data[key].senderName,
              senderAvatar: data[key].senderId === user.id.toString() ? user.avatar : otherUser.avatar,
            }));
            setMessages(messageList.reverse());
          } else {
            setMessages([]);
          }
          setLoading(false);
        },
        (error) => {
          console.error("Lỗi khi lắng nghe tin nhắn:", error);
          setError("Không thể tải tin nhắn. Vui lòng thử lại.");
          setLoading(false);
        }
      );
    } else {
      setMessages([]);
      setLoading(false);
      setError("Vui lòng đăng nhập và cung cấp ID người dùng hợp lệ.");
    }
    return () => {
      if (unsubscribe && chatRef) off(chatRef);
    };
  }, [user, otherUserId, otherUser.avatar]);

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
      setImage(null); // Đảm bảo chỉ chọn file hoặc ảnh
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
      setFile(null); // Đảm bảo chỉ chọn ảnh hoặc file
    }
  };

  // Gửi tin nhắn, file hoặc ảnh
  const onSend = useCallback(async () => {
    if (!user?.id || !otherUserId) return;
    if (!newMessage.trim() && !file && !image) return;

    try {
      setLoading(true);
      setFileUploading(true);

      const chatRef = ref(db, `chat_rooms/${user.role === "PATIENT" ? `${user.id}_${otherUserId}` : `${otherUserId}_${user.id}`}/messages`);
      const newMessageRef = push(chatRef);
      let messageData = {
        senderId: user.id.toString(),
        senderName: user.firstName || user.username || "Người dùng",
        timestamp: Date.now(),
      };

      // Xử lý gửi file
      if (file) {
        const filePath = `chat_files/${user.role === "PATIENT" ? `${user.id}_${otherUserId}` : `${otherUserId}_${user.id}`}/${Date.now()}_${file.name}`;
        const fileStorageRef = storageRef(storage, filePath);
        await uploadBytes(fileStorageRef, file);
        const fileUrl = await getDownloadURL(fileStorageRef);

        messageData.type = "file";
        messageData.fileUrl = fileUrl;
        messageData.fileName = file.name;
        if (newMessage.trim()) {
          messageData.text = newMessage.trim();
        }
      }
      // Xử lý gửi ảnh
      else if (image) {
        const imagePath = `chat_images/${user.role === "PATIENT" ? `${user.id}_${otherUserId}` : `${otherUserId}_${user.id}`}/${Date.now()}_${image.name}`;
        const imageStorageRef = storageRef(storage, imagePath);
        await uploadBytes(imageStorageRef, image);
        const imageUrl = await getDownloadURL(imageStorageRef);

        messageData.type = "image";
        messageData.fileUrl = imageUrl;
        messageData.fileName = image.name;
        if (newMessage.trim()) {
          messageData.text = newMessage.trim();
        }
      }
      // Gửi tin nhắn văn bản
      else {
        messageData.type = "text";
        messageData.text = newMessage.trim();
      }

      await set(newMessageRef, messageData);

      const participantsRef = ref(db, `chat_rooms/${user.role === "PATIENT" ? `${user.id}_${otherUserId}` : `${otherUserId}_${user.id}`}/participants`);
      await set(participantsRef, {
        userId: user.id.toString(),
        otherUserId: otherUserId.toString(),
      });

      setNewMessage("");
      setFile(null);
      setImage(null);
      if (fileInputRef.current) fileInputRef.current.value = "";
      if (imageInputRef.current) imageInputRef.current.value = "";
    } catch (err) {
      console.error("Lỗi khi gửi tin nhắn, tệp hoặc ảnh:", err);
      setError(`Không thể gửi tin nhắn, tệp hoặc ảnh: ${err.message}`);
    } finally {
      setLoading(false);
      setFileUploading(false);
    }
  }, [user, otherUserId, newMessage, file, image]);

  // Kích hoạt input file
  const triggerFileInput = () => {
    fileInputRef.current?.click();
  };

  // Kích hoạt input ảnh
  const triggerImageInput = () => {
    imageInputRef.current?.click();
  };

  const handleVideoCall = () => {
    alert("Tính năng gọi video sẽ được triển khai sau!");
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
      <Card className="shadow border-0" style={{ maxWidth: "600px", margin: "0 auto" }}>
        <Card.Header className="bg-primary text-white d-flex align-items-center justify-content-between p-3">
          <div className="d-flex align-items-center">
            <img
              src={otherUser.avatar || "/images/placeholder.jpg"}
              alt={otherUser.name}
              style={{ width: "40px", height: "40px", borderRadius: "50%", objectFit: "cover", marginRight: "10px" }}
            />
            <h4 className="mb-0">Chat với {otherUser.name}</h4>
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
              className="chat-messages p-3"
              style={{ height: "400px", overflowY: "auto", backgroundColor: "#f8f9fa" }}
            >
              {messages.map((msg) => {
                const isCurrentUser = msg.senderId === user.id.toString();
                return (
                  <div
                    key={msg.id}
                    className={`d-flex mb-3 ${isCurrentUser ? "justify-content-end" : "justify-content-start"}`}
                  >
                    {!isCurrentUser && (
                      <img
                        src={msg.senderAvatar || "/images/placeholder.jpg"}
                        alt={msg.senderName}
                        style={{ width: "30px", height: "30px", borderRadius: "50%", objectFit: "cover", marginRight: "10px", alignSelf: "flex-end" }}
                      />
                    )}
                    <div
                      className={`p-2 ${isCurrentUser ? "bg-primary text-white" : "bg-light text-dark"}`}
                      style={{ borderRadius: "8px", maxWidth: "70%", boxShadow: "0 1px 3px rgba(0,0,0,0.1)" }}
                    >
                      <small className="d-block" style={{ opacity: "0.7", fontSize: "0.8em" }}>
                        {msg.senderName}
                      </small>
                      {msg.type === "image" && (
                        <img
                          src={msg.fileUrl}
                          alt="Ảnh đã gửi"
                          className="img-fluid rounded mb-1"
                          style={{ maxHeight: "150px" }}
                        />
                      )}
                      {msg.type === "file" && (
                        <a
                          href={msg.fileUrl}
                          download={msg.fileName}
                          className="d-flex align-items-center text-primary mb-1"
                          style={{ textDecoration: "none" }}
                        >
                          <svg
                            className="me-1"
                            width="16"
                            height="16"
                            fill="currentColor"
                            viewBox="0 0 16 16"
                            xmlns="http://www.w3.org/2000/svg"
                          >
                            <path d="M14.5 3h-4.5l-1-1h-5a1.5 1.5 0 0 0-1.5 1.5v10a1.5 1.5 0 0 0 1.5 1.5h11a1.5 1.5 0 0 0 1.5-1.5v-9.5a1.5 1.5 0 0 0-1.5-1.5zm-11 1.5a.5.5 0 0 1 .5-.5h4.793l1 1h4.707a.5.5 0 0 1 .5.5v9.5a.5.5 0 0 1-.5.5h-11a.5.5 0 0 1-.5-.5v-10z"/>
                          </svg>
                          {msg.fileName}
                        </a>
                      )}
                      {msg.text && <p className="mb-1">{msg.text}</p>}
                      <small className="d-block" style={{ opacity: "0.5", fontSize: "0.7em" }}>
                        {msg.createdAt.toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" })}
                      </small>
                    </div>
                    {isCurrentUser && (
                      <img
                        src={user.avatar || "/images/placeholder.jpg"}
                        alt={user.firstName || user.username}
                        style={{ width: "30px", height: "30px", borderRadius: "50%", objectFit: "cover", marginLeft: "10px", alignSelf: "flex-end" }}
                      />
                    )}
                  </div>
                );
              })}
              <div ref={messagesEndRef} />
            </div>
          )}
        </Card.Body>
        <Card.Footer className="p-3">
          <Form
            className="d-flex align-items-center"
            onSubmit={(e) => {
              e.preventDefault();
              onSend();
            }}
          >
            {/* Input cho file */}
            <Form.Control
              type="file"
              ref={fileInputRef}
              onChange={handleFileChange}
              accept=".pdf,.doc,.docx"
              style={{ display: "none" }}
              aria-label="Chọn tệp để gửi"
            />
            <Button
              variant="outline-secondary"
              size="sm"
              className="me-2"
              onClick={triggerFileInput}
              disabled={loading || fileUploading || !user.id || !otherUserId}
              aria-label="Gửi tệp"
              style={{ borderRadius: "20px" }}
            >
              {fileUploading && file ? (
                <Spinner animation="border" size="sm" />
              ) : (
                <svg
                  width="16"
                  height="16"
                  fill="currentColor"
                  viewBox="0 0 16 16"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path d="M8 3a.5.5 0 0 1 .5.5v3h3a.5.5 0 0 1 0 1h-3v3a.5.5 0 0 1-1 0v-3h-3a.5.5 0 0 1 0-1h3v-3A.5.5 0 0 1 8 3z"/>
                </svg>
              )}
            </Button>
            {/* Input cho ảnh */}
            <Form.Control
              type="file"
              ref={imageInputRef}
              onChange={handleImageChange}
              accept="image/*"
              style={{ display: "none" }}
              aria-label="Chọn ảnh để gửi"
            />
            <Button
              variant="outline-secondary"
              size="sm"
              className="me-2"
              onClick={triggerImageInput}
              disabled={loading || fileUploading || !user.id || !otherUserId}
              aria-label="Gửi ảnh"
              style={{ borderRadius: "20px" }}
            >
              {fileUploading && image ? (
                <Spinner animation="border" size="sm" />
              ) : (
                <svg
                  width="16"
                  height="16"
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
              style={{ borderRadius: "20px" }}
            />
            <Button
              variant="primary"
              onClick={onSend}
              disabled={loading || fileUploading || !user.id || !otherUserId || (!newMessage.trim() && !file && !image)}
              aria-label="Gửi tin nhắn, tệp hoặc ảnh"
              style={{ borderRadius: "20px" }}
            >
              {loading ? <Spinner animation="border" size="sm" /> : "Gửi"}
            </Button>
          </Form>
        </Card.Footer>
      </Card>
    </div>
  );
};

export default ChatRoom;
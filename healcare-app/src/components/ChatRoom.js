import { useEffect, useState, useRef, useCallback } from "react";
import { ref, onValue, push, set, off } from "firebase/database";
import { db } from "../configs/firebase";
import { Alert, Button, Form, Spinner } from "react-bootstrap";
import { useMyUser } from "../configs/MyContexts";
import { useParams } from "react-router-dom";
import Apis, { authApis, endpoints } from "../configs/Apis";

const ChatRoom = () => {
  const { otherUserId } = useParams(); // Lấy từ URL
  const { user } = useMyUser() || {};
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [otherUserName, setOtherUserName] = useState("Người dùng không xác định");
  const messagesEndRef = useRef(null);

  // Cuộn đến tin nhắn mới nhất
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };


  // Lấy thông tin người dùng khác (otherUserName) từ API
  useEffect(() => {
    const fetchOtherUserName = async () => {
      if (!otherUserId) return;

      try {

        let userData;
        if (user.role === "PATIENT") {
          const res = await Apis.get(`${endpoints['doctors']}/${otherUserId}`);
          userData = res.data;
        } else {
          const res = await Apis.get(`${endpoints['patients']}/${otherUserId}`);
          userData = res.data;
        }

        setOtherUserName(
          `${userData.first_name || ""} ${userData.last_name || ""}`.trim() || otherUserId
        );
      } catch (error) {
        console.error("Lỗi khi lấy thông tin người dùng khác:", error);
      }
    };

    fetchOtherUserName();
  }, [otherUserId, user]);

  // Lắng nghe tin nhắn từ Realtime Database
  useEffect(() => {
    let unsubscribe;
    let chatRef = null; // Khai báo chatRef ở đây để có phạm vi ngoài if

    if (user.id && otherUserId) {
        if (user.role === 'PATIENT'){
            chatRef = ref(db, `chat_rooms/${user.id}_${otherUserId}/messages`);
        }else{
            chatRef = ref(db, `chat_rooms/${otherUserId}_${user.id}/messages`);
        }
      unsubscribe = onValue(
        chatRef,
        (snapshot) => {
          const data = snapshot.val();
          if (data) {
            const messageList = Object.keys(data).map((key) => ({
              id: key,
              text: data[key].text,
              createdAt: new Date(data[key].timestamp),
              senderId: data[key].senderId,
              senderName: data[key].senderName,
            }));
            setMessages(messageList.reverse());
          } else {
            setMessages([]);
          }
          setLoading(false);
          scrollToBottom();
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
    }

    return () => {
      if (unsubscribe && chatRef) off(chatRef); // Kiểm tra chatRef trước khi gọi off
    };
  }, [user.id, otherUserId]);

  // Gửi tin nhắn
  const onSend = useCallback(async () => {
    if (!newMessage.trim() || !user.id || !otherUserId) return;

    try {
      setLoading(true);
      
       if (user.role === 'PATIENT'){
            var chatRef = ref(db, `chat_rooms/${user.id}_${otherUserId}/messages`);
        }else{
            var chatRef = ref(db, `chat_rooms/${otherUserId}_${user.id}/messages`);
        }
      const newMessageRef = push(chatRef);
      await set(newMessageRef, {
        senderId: user.id.toString(),
        senderName: user.firstName || user.username || "Người dùng",
        text: newMessage.trim(),
        timestamp: Date.now(),
      });

      const participantsRef = ref(db, `chat_rooms/${user.id}_${otherUserId}/participants`);
      await set(participantsRef, {
        userId: user.id.toString(),
        otherUserId: otherUserId.toString(),
      });

      setNewMessage("");
    } catch (err) {
      console.error("Lỗi khi gửi tin nhắn:", err);
      setError("Không thể gửi tin nhắn. Vui lòng thử lại.");
    } finally {
      setLoading(false);
    }
  }, [user, otherUserId, newMessage]);

  return (
    <div className="chat-room p-3" style={{ maxWidth: "600px", margin: "0 auto" }}>
      <h4 className="text-primary mb-4">Chat với {otherUserName}</h4>
      {error && <Alert variant="danger" dismissible onClose={() => setError(null)}>{error}</Alert>}
      {loading && (
        <div className="text-center">
          <Spinner animation="border" variant="primary" />
        </div>
      )}
      {!loading && (
        <div
          className="chat-messages"
          style={{
            height: "400px",
            overflowY: "auto",
            backgroundColor: "#f8f9fa",
            borderRadius: "8px",
            padding: "15px",
          }}
        >
          {messages.map((msg) => {
            const isCurrentUser = msg.senderId === user.id.toString();
            return (
              <div
                key={msg.id}
                className={`mb-2 ${isCurrentUser ? "text-right" : "text-left"}`}
              >
                <div
                  className={`d-inline-block p-2 ${
                    isCurrentUser ? "bg-primary text-white" : "bg-light"
                  }`}
                  style={{
                    borderRadius: "8px",
                    maxWidth: "70%",
                  }}
                >
                  <small className="text-muted mb-1 d-block">{msg.senderName}</small>
                  <p>{msg.text}</p>
                  <small className="text-muted">
                    {msg.createdAt.toLocaleTimeString("vi-VN", {
                      hour: "2-digit",
                      minute: "2-digit",
                    })}
                  </small>
                </div>
              </div>
            );
          })}
          <div ref={messagesEndRef} />
        </div>
      )}
      <Form className="mt-3 d-flex" onSubmit={(e) => { e.preventDefault(); onSend(); }}>
        <Form.Control
          type="text"
          value={newMessage}
          onChange={(e) => setNewMessage(e.target.value)}
          placeholder="Nhập tin nhắn..."
          disabled={loading || !user.id || !otherUserId}
          style={{ flex: "1", marginRight: "10px" }}
        />
        <Button
          variant="primary"
          onClick={onSend}
          disabled={loading || !user.id || !otherUserId || !newMessage.trim()}
        >
          {loading ? <Spinner animation="border" size="sm" /> : "Gửi"}
        </Button>
      </Form>
    </div>
  );
};

export default ChatRoom;
import { initializeApp } from 'firebase/app';
import { getDatabase } from 'firebase/database';
import { getStorage } from "firebase/storage";

const firebaseConfig = {
  apiKey: "AIzaSyCVBO9WchCoo5zbKhww_8O8S-w8J0DR3gM",
  authDomain: "healcarechat.firebaseapp.com",
  projectId: "healcarechat",
  storageBucket: "healcarechat.firebasestorage.app",
  messagingSenderId: "394145538685",
  appId: "1:394145538685:web:8bd4d1ea9dc710ac7d0714",
  measurementId: "G-FMJR7R62TJ",
  databaseURL: "https://healcarechat-default-rtdb.firebaseio.com/"
};

// try {
//   if (!firebase.apps.length) {
//     firebase.initializeApp(firebaseConfig);
//   }
// } catch (error) {
//   console.error('Lỗi khởi tạo Firebase:', error);
// }

const app = initializeApp(firebaseConfig);
const db = getDatabase(app);
const storage = getStorage(app);

export { db, storage };

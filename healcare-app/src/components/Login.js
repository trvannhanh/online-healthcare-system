import { useContext, useRef, useState } from "react";
import { Alert, Button, Form } from "react-bootstrap";
import Apis, { authApis, endpoints } from "../configs/Apis";
import MySpinner from "./layout/MySpinner";
import { useNavigate } from "react-router-dom";
import cookie from 'react-cookies'
import { MyDispatcherContext } from "../configs/MyContexts";

const Login = () => {

    const info = [{
        title: "Tên đăng nhập",
        field: "username",
        type: "text"
    }, {
        title: "Mật khẩu",
        field: "password",
        type: "password"
    }];
    const [user, setUser] = useState({});
    const [msg, setMsg] = useState();
    const [loading, setLoading] = useState(false);
    const nav = useNavigate();
    const dispatch = useContext(MyDispatcherContext);

    const setState = (value, field) => {
        setUser({...user, [field]: value});
    }
    
    const login = async (e) => {
        e.preventDefault();
        try {
            setLoading(true);
            let res = await Apis.post(endpoints['login'], {
                ...user
            });
           
            cookie.save('token', res.data.token);

            let u = await authApis().get(endpoints['current-user']);
            console.info(u.data);

            dispatch({
                "type": "login",
                "payload": u.data
            });
            nav("/");
        } catch (ex) {
            console.error(ex);
        } finally {
            setLoading(false);
        }
    }
    return (
        <>
            <h1 className="text-center text-success mt-1">ĐĂNG KÝ</h1>

            {msg && <Alert variant="danger">{msg}</Alert>}

            <Form onSubmit={login}>
                {info.map(i => <Form.Control value={user[i.field]} onChange={e => setState(e.target.value, i.field)} className="mt-3 mb-1" key={i.field} type={i.type} placeholder={i.title} required />)}

                {loading === true?<MySpinner />:<Button type="submit" variant="success" className="mt-3 mb-1">Đăng nhập</Button>}
            </Form>  
        </>
    );

} 

export default Login;
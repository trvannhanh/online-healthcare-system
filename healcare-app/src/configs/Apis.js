import axios from "axios";

const BASE_URL = 'http://localhost:8080/HealCareApp/api';

export const endpoint = {
    'hospitals': '/hospitals',
    'specialization': '/specialization',
    'doctors': '/doctors',
    'appointments': '/appointments'
}

export default axios.create({
    baseURL: BASE_URL
})
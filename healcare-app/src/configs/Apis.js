import axios from "axios";
import cookie from 'react-cookies'

const BASE_URL = 'http://localhost:8080/HealCareApp/api';

export const endpoints = {
    'hospitals': '/hospitals',
    'specialization': '/specialization',
    'doctors': '/doctors',
    'appointments': '/appointments',
    'register': '/users',
    'login': '/login',
    'current-user': '/secure/profile',
    'appointmentsFilter': '/appointments/filter',
    'cancelAppointment': (id) => `/secure/appointments/${id}/cancel`,
    'rescheduleAppointment': (id) => `/secure/appointments/${id}/reschedule`,
}

export const authApis = () => {
    return axios.create({
        baseURL: BASE_URL,
        headers: {
            'Authorization': `Bearer ${cookie.load('token')}`
        }
    })
}

export default axios.create({
    baseURL: BASE_URL
})
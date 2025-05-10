import React, { useState, useEffect } from 'react';
const PatientProfile = () => {

    const [patientInfo, setPatientInfo] = useState({

    });

    return (
        <div style={styles.container}>
            <h1 style={styles.title}>Quản Lý Hồ Sơ Bệnh Nhân</h1>
            <form style={styles.form} onSubmit={handleSubmit}>
                <div style={styles.formGroup}>
                    <label style={styles.label}>Họ:</label>
                    <input
                        type="text"
                        name="name"
                        value={patientInfo.name}
                        onChange={handleChange}
                        style={styles.input}
                    />
                </div>
                <div style={styles.formGroup}>
                    <label style={styles.label}>Tên:</label>
                    <input
                        type="text"
                        name="name"
                        value={patientInfo.name}
                        onChange={handleChange}
                        style={styles.input}
                    />
                </div>
                <div style={styles.formGroup}>
                    <label style={styles.label}>Tuổi:</label>
                    <input
                        type="number"
                        name="age"
                        value={patientInfo.age}
                        onChange={handleChange}
                        style={styles.input}
                    />
                </div>
                <div style={styles.formGroup}>
                    <label style={styles.label}>Giới Tính:</label>
                    <select
                        name="gender"
                        value={patientInfo.gender}
                        onChange={handleChange}
                        style={styles.select}
                    >
                        <option value="Nam">Nam</option>
                        <option value="Nữ">Nữ</option>
                        <option value="Khác">Khác</option>
                    </select>
                </div>
                <div style={styles.formGroup}>
                    <label style={styles.label}>Điện Thoại:</label>
                    <input
                        type="text"
                        name="phone"
                        value={patientInfo.phone}
                        onChange={handleChange}
                        style={styles.input}
                    />
                </div>
                <div style={styles.formGroup}>
                    <label style={styles.label}>Email:</label>
                    <input
                        type="email"
                        name="email"
                        value={patientInfo.email}
                        onChange={handleChange}
                        style={styles.input}
                    />
                </div>
                <div style={styles.formGroup}>
                    <label style={styles.label}>Địa Chỉ:</label>
                    <input
                        type="text"
                        name="address"
                        value={patientInfo.address}
                        onChange={handleChange}
                        style={styles.input}
                    />
                </div>
                <button type="submit">Cập Nhật Thông Tin</button>
            </form>
        </div>
    );
};
export default PatientProfile;
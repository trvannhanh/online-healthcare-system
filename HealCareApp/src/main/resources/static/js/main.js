function deleteAppointment(endpoint, id) {
    if (confirm("Ban chac chan xoa khong?")) {
        fetch(`${endpoint}/${id}`, {
            method: 'delete'
        }).then(res => {
            if (res.status === 204) {
                alert("Xoa thanh cong!");
                location.reload(); //Khong thuc hien ben react
            } else{
                 alert("He thong bi loi!");
            }
        });
    }
}

function deleteNotification(endpoint, notificationId) {
    if (confirm("Bạn có chắc chắn muốn xóa thông báo này?")) {
        fetch(`${endpoint}/${notificationId}`, {
            method: "DELETE",
        })
            .then((response) => {
                if (response.ok) {
                    alert("Xóa thông báo thành công!");
                    location.reload();
                } else {
                    alert("Xóa thông báo thất bại!");
                }
            })
            .catch((error) => console.error("Error:", error));
    }
}
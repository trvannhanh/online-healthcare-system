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
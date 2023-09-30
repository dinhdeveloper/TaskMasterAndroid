package com.elogictics.taskmaster.model

enum class JobStateCode {
    NEW,   //Mới
    ASSIGNED,  //Đã giao
    ACCEPTED,  //Chấp nhận
    COMPACTED,  //Đã làm gọn
    WEIGHTED,  //Đã cân
    ON_TRUCK,  //Đã lên xe
    DONE,  //Xong
    REJECTED,  //Từ chối
    CANCELED  //Hủy

}
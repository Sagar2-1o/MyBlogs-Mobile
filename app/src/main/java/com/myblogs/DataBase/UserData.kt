package com.myblogs.DataBase

data class UserData(
    var userId: String = "",
    var name: String = "",
    var email: String = "",
    var password: String = "",
    var profileImageUrl: String = ""
)


data class NewBlogData(
    val blogId: String = "",
    val userId: String = "",
    val userName: String = "", // add this
    val title: String = "",
    val description: String = "",
    val imageUrl: String = ""
)



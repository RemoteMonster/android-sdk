package com.remotemonster.example.simpleconference

class Message(user: String, message: String) {

    var user: String? = user
    var message: String? = message

    constructor() : this("" , "")
}

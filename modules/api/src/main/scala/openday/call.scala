package openday

import upickle.default.*

enum Player derives ReadWriter {
    case Red
    case Blue
}

enum Language derives ReadWriter {
    case Ping
    case Pong
    case SendBoard
}


def fromJson(s:String):Language = read[Language](s)
def toJson(l:Language):String = write(l)
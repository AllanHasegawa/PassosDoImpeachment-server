package com.hasegawa.di.passgen

import org.mindrot.jbcrypt.BCrypt
import java.util.*

/**
 * Created by hasegawa on 3/26/2016.
 */
class MainApp {
    companion object {
        @JvmStatic fun main(args: Array<String>) {

            if (args.size != 3) {
                println("With gradle, use: -PappArgs=\"['<username>', '<password>', 'role']\" to pass arguments.")
                error("Pass the password you want to hash/salt as argument to this program.")
            }

            val id = UUID.randomUUID().toString()
            val username = args[0]
            val hash = BCrypt.hashpw(args[1], BCrypt.gensalt())
            val role_level = when (args[2].toUpperCase()) {
                "USER" -> 0
                "EDITOR" -> 0x42
                "ADMIN" -> 0x9000
                else -> throw RuntimeException("Role unknown.")
            }

            println("insert into users (id, username, password_hash, role_level, public_name) " +
                    "values ('$id', '$username', '$hash', $role_level, '$username')"
            )
        }
    }
}
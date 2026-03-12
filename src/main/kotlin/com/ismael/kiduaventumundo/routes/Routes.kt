package com.ismael.kiduaventumundo.routes

import com.ismael.kiduaventumundo.dto.*
import com.ismael.kiduaventumundo.model.ProgressEvent
import com.ismael.kiduaventumundo.model.SessionRow
import com.ismael.kiduaventumundo.model.User
import com.ismael.kiduaventumundo.model.UserActivityProgress
import com.ismael.kiduaventumundo.model.UserLevelProgress
import com.ismael.kiduaventumundo.service.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing

fun Application.registerRoutes(userService: UserService) {
    routing {
        get("/") {
            call.respond(HttpStatusCode.OK, MessageResponse("felicidades acabas de crear tu primer API"))
        }

        get("/health") {
            call.respond(HttpStatusCode.OK, HealthResponse())
        }

        post("/users") {
            val payload = call.receive<User>()
            val created = userService.register(payload)
            call.respond(HttpStatusCode.Created, created)
        }

        get("/users/{nickname}") {
            val nickname = call.parameters["nickname"].orEmpty()
            call.respond(HttpStatusCode.OK, userService.getByNickname(nickname))
        }

        get("/users/id/{userId}") {
            call.respond(HttpStatusCode.OK, userService.getById(call.longPathParam("userId")))
        }

        post("/login") {
            val payload = call.receive<LoginRequest>()
            call.respond(HttpStatusCode.OK, userService.login(payload))
        }

        put("/users/{userId}/avatar") {
            val userId = call.longPathParam("userId")
            val payload = call.receive<AvatarUpdateRequest>()
            call.respond(HttpStatusCode.OK, userService.updateAvatar(userId, payload))
        }

        put("/users/{userId}/password") {
            val userId = call.longPathParam("userId")
            val payload = call.receive<PasswordUpdateRequest>()
            call.respond(HttpStatusCode.OK, userService.updatePassword(userId, payload))
        }

        get("/session") {
            call.respond(HttpStatusCode.OK, userService.getSession())
        }

        put("/session") {
            val payload = call.receive<SessionRow>()
            call.respond(HttpStatusCode.OK, userService.updateSession(payload))
        }

        delete("/session") {
            call.respond(HttpStatusCode.OK, userService.clearSession())
        }

        get("/english/levels") {
            call.respond(HttpStatusCode.OK, userService.getEnglishLevels())
        }

        get("/english/levels/{level}/activities") {
            call.respond(HttpStatusCode.OK, userService.getEnglishActivities(call.intPathParam("level")))
        }

        get("/users/{userId}/progress/levels") {
            call.respond(HttpStatusCode.OK, userService.getLevelProgress(call.longPathParam("userId")))
        }

        get("/users/{userId}/progress/levels/{level}") {
            call.respond(
                HttpStatusCode.OK,
                userService.getLevelProgress(call.longPathParam("userId"), call.intPathParam("level"))
            )
        }

        put("/users/{userId}/progress/levels/{level}") {
            val userId = call.longPathParam("userId")
            val level = call.intPathParam("level")
            val body = call.receive<UserLevelProgress>()
            call.respond(HttpStatusCode.OK, userService.upsertLevelProgress(userId, level, body))
        }

        get("/users/{userId}/progress/levels/{level}/activities") {
            call.respond(
                HttpStatusCode.OK,
                userService.getActivityProgress(call.longPathParam("userId"), call.intPathParam("level"))
            )
        }

        put("/users/{userId}/progress/levels/{level}/activities/{activity_index}") {
            val userId = call.longPathParam("userId")
            val level = call.intPathParam("level")
            val activityIndex = call.intPathParam("activity_index")
            val body = call.receive<UserActivityProgress>()
            call.respond(HttpStatusCode.OK, userService.upsertActivityProgress(userId, level, activityIndex, body))
        }

        post("/users/{userId}/progress/events") {
            val userId = call.longPathParam("userId")
            val body = call.receive<ProgressEvent>()
            call.respond(HttpStatusCode.Created, userService.createProgressEvent(userId, body))
        }

        get("/users/{userId}/progress/events") {
            call.respond(HttpStatusCode.OK, userService.getProgressEvents(call.longPathParam("userId")))
        }

        get("/users/{userId}/progress/summary") {
            call.respond(HttpStatusCode.OK, userService.getProgressSummary(call.longPathParam("userId")))
        }
    }
}

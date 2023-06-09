package ua.student.bot.telegrambot.Config.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;


import java.sql.Timestamp;

@Data
@Entity(name = "usersData")
public class User {
    @Id
    @Column (name = "id",nullable = false)
    private Long chatId;

    private String firstName;

    private String lastName;

    private String userName;

    private Timestamp registration;

}

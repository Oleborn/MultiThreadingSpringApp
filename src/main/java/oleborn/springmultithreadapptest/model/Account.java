package oleborn.springmultithreadapptest.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data // Генерирует геттеры, сеттеры, equals, hashCode и toString.
@NoArgsConstructor // Создает пустой конструктор.
@AllArgsConstructor // Создает конструктор со всеми аргументами.
@Builder // Позволяет использовать паттерн "строитель" для создания объектов.
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Автоматическая генерация ID.
    private Long id;

    @Column(nullable = false, unique = true) // Поле не может быть null и должно быть уникальным.
    private String accountNumber;

    @Column(nullable = false) // Поле не может быть null.
    private BigDecimal balance;

    @Column(nullable = false)
    private String ownerName;

    @Version
    // Используется для контроля версий записей (оптимистическая блокировка, обеспечивает защиту от одновременного изменения данных).
    private int version;
}

package avr.dto;

public record DatabaseConfigDTO(
    String name,
    boolean enabled,
    String jdbcUrl,
    String username) {
}

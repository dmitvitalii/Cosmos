package me.dmitvitalii.cosmos;

/**
 * @author Vitalii Dmitriev
 * @since 22.03.2017
 */
public interface Command {
    void exec(String... params);
}

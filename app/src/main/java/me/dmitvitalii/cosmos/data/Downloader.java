package me.dmitvitalii.cosmos.data;

import retrofit2.Callback;

/**
 * @author Vitalii Dmitriev
 * @since 22.03.2017
 */
public interface Downloader<T> {
    void download(Callback<T> callback, String... params);
}

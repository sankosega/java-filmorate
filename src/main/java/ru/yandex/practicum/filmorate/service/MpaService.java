package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class MpaService {
    private final Map<Integer, Mpa> mpaRatings = new LinkedHashMap<>();

    public MpaService() {
        mpaRatings.put(1, new Mpa(1, "G"));
        mpaRatings.put(2, new Mpa(2, "PG"));
        mpaRatings.put(3, new Mpa(3, "PG-13"));
        mpaRatings.put(4, new Mpa(4, "R"));
        mpaRatings.put(5, new Mpa(5, "NC-17"));
    }

    public Collection<Mpa> findAll() {
        return mpaRatings.values();
    }

    public Mpa findById(Integer id) {
        Mpa mpa = mpaRatings.get(id);
        if (mpa == null) {
            throw new NotFoundException("MPA с id=" + id + " не найден");
        }
        return mpa;
    }
}

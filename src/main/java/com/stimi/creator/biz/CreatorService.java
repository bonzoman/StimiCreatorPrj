package com.stimi.creator.biz;

import com.stimi.creator.sql.CreatorQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreatorService {
    private final CreatorQuery creatorQuery;

}
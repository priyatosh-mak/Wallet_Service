package com.mightyfin.WalletService.Dto.response;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class PageResponse<T> {


    private List<T> items;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {

        PageResponse<T> response = new PageResponse<>();

        response.setItems(
                page.getContent().stream()
                        .map(mapper)
                        .toList()
        );

        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());

        return response;
    }
}

package com.cmcglobal.backend.mapper;

import com.cmcglobal.backend.dto.response.dotInfoByTime.DotInfoByTimeDTO;
import com.cmcglobal.backend.entity.DotInfoByTime;

import java.util.List;

public interface DotInfoByTimeMapper{
    DotInfoByTime toEntity(DotInfoByTimeDTO dto);

    DotInfoByTimeDTO toDTO(DotInfoByTime entity);

    List<DotInfoByTime> toListEntity(List<DotInfoByTimeDTO> listDto);

    List<DotInfoByTimeDTO> toListDTO(List<DotInfoByTime> listEntity);
}

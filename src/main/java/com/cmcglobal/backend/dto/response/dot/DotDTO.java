package com.cmcglobal.backend.dto.response.dot;

import com.cmcglobal.backend.dto.response.dotInfoByTime.DotInfoByTimeDTO;
import com.cmcglobal.backend.entity.DotInfoByTime;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class DotDTO {
    private Integer id;
    private String userId;
    private String username;
    private String fullName;
    private String owner;
    private String department;
    private String group;
    private Float coordinateX;
    private Float coordinateY;
    private String fromDate;
    private String toDate;
    private String status;
    private String type;
    private Boolean isActive;
    private String floorName;
    private Integer floorId;
    private String buildingName;
    private Integer buildingId;
    private Float price;
    private List<DotInfoByTimeDTO> dotInfoByTimeDTOList;
}

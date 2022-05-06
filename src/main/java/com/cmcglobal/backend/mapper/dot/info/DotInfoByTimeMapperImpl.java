package com.cmcglobal.backend.mapper.dot.info;

import com.cmcglobal.backend.dto.response.dotInfoByTime.DotInfoByTimeDTO;
import com.cmcglobal.backend.entity.DotInfoByTime;
import com.cmcglobal.backend.entity.UserFlattened;
import com.cmcglobal.backend.mapper.DotInfoByTimeMapper;
import com.cmcglobal.backend.repository.UserFlattenedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class DotInfoByTimeMapperImpl implements DotInfoByTimeMapper {

    @Autowired
    private UserFlattenedRepository userFlattenedRepository;

    @Override
    public DotInfoByTime toEntity(DotInfoByTimeDTO dto) {
        if ( dto == null ) {
            return null;
        }

        DotInfoByTime dotInfoByTime = new DotInfoByTime();

        dotInfoByTime.setId( dto.getId() );
        dotInfoByTime.setStatus( dto.getStatus() );
        dotInfoByTime.setFromDate( dto.getFromDate() );
        dotInfoByTime.setToDate( dto.getToDate() );
        dotInfoByTime.setMember( dto.getMember() );
        dotInfoByTime.setOwner( dto.getOwner() );

        return dotInfoByTime;
    }

    @Override
    public DotInfoByTimeDTO toDTO(DotInfoByTime entity) {
        if ( entity == null ) {
            return null;
        }

        DotInfoByTimeDTO dotInfoByTimeDTO = new DotInfoByTimeDTO();

        // MANUAL CODE
        if (entity.getMember() != null) {
            UserFlattened user = userFlattenedRepository.findByUserName(entity.getMember());
            if (user != null) {
                dotInfoByTimeDTO.setUserId(user.getId());
                dotInfoByTimeDTO.setUsername(user.getUserName());
                dotInfoByTimeDTO.setFullName(user.getFullName());
            }
        }
        if (entity.getOwner() != null) {
            UserFlattened owner = userFlattenedRepository.findByUserName(entity.getOwner());
            if (owner != null) {
                dotInfoByTimeDTO.setOwnerFullName(owner.getFullName());
                dotInfoByTimeDTO.setDepartment(owner.getDepartmentName());
                dotInfoByTimeDTO.setGroup(owner.getParentDepartmentName() != null ? owner.getParentDepartmentName() : owner.getDepartmentName());
            }
        }

        dotInfoByTimeDTO.setId( entity.getId() );
        dotInfoByTimeDTO.setStatus( entity.getStatus() );
        dotInfoByTimeDTO.setFromDate( entity.getFromDate() );
        dotInfoByTimeDTO.setToDate( entity.getToDate() );
        dotInfoByTimeDTO.setMember( entity.getMember() );
        dotInfoByTimeDTO.setOwner( entity.getOwner() );

        return dotInfoByTimeDTO;
    }

    @Override
    public List<DotInfoByTime> toListEntity(List<DotInfoByTimeDTO> listDto) {
        if ( listDto == null ) {
            return null;
        }

        List<DotInfoByTime> list = new ArrayList<DotInfoByTime>( listDto.size() );
        for ( DotInfoByTimeDTO dotInfoByTimeDTO : listDto ) {
            list.add( toEntity( dotInfoByTimeDTO ) );
        }

        return list;
    }

    @Override
    public List<DotInfoByTimeDTO> toListDTO(List<DotInfoByTime> listEntity) {
        if ( listEntity == null ) {
            return null;
        }

        List<DotInfoByTimeDTO> list = new ArrayList<DotInfoByTimeDTO>( listEntity.size() );
        for ( DotInfoByTime dotInfoByTime : listEntity ) {
            list.add( toDTO( dotInfoByTime ) );
        }

        return list;
    }
}

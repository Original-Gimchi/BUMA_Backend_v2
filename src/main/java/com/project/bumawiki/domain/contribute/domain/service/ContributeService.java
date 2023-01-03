package com.project.bumawiki.domain.contribute.domain.service;

import com.project.bumawiki.domain.contribute.domain.Contribute;
import com.project.bumawiki.domain.docs.domain.Docs;
import com.project.bumawiki.domain.user.entity.User;
import com.project.bumawiki.domain.user.exception.UserNotFoundException;
import com.project.bumawiki.domain.user.presentation.dto.UserResponseDto;
import com.project.bumawiki.global.annotation.ServiceWithTransactionalReadOnly;
import com.project.bumawiki.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;

@ServiceWithTransactionalReadOnly
@RequiredArgsConstructor
public class ContributeService {

    @Transactional
    public void setContribute(Docs docs) {
        User user = SecurityUtil.getCurrentUser().getUser();
        if(user == null){
            throw UserNotFoundException.EXCEPTION;
        }
        Contribute contribute = Contribute.builder()
                .docs(docs)
                .contributor(user)
                .createdAt(LocalDateTime.now())
                .build();
        ArrayList<Contribute> contributes = new ArrayList<>();
        contributes.add(contribute);
        docs.setContributor(contributes);
        user.setContributeDocs(contributes);
    }

//    @Transactional
//    private void setContribute(Docs docs, UserResponseDto userResponseDto) {
//        User user = SecurityUtil.getCurrentUser().getUser();
//        Contribute contribute = Contribute.builder()
//                .docs(docs)
//                .contributor(user)
//                .createdAt(LocalDateTime.now())
//                .build();
//        userResponseDto.updateContribute(contribute);
//        docs.updateContribute(contribute);
//
//        user.setContributeDocs(userResponseDto.getContributeDocs());
//    }
}

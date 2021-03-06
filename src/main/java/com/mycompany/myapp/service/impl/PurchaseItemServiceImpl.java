package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.PurchaseItem;
import com.mycompany.myapp.repository.PurchaseItemRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.service.PurchaseItemService;
import com.mycompany.myapp.service.dto.PurchaseItemDTO;
import com.mycompany.myapp.service.mapper.PurchaseItemMapper;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PurchaseItemServiceImpl implements PurchaseItemService {

    private final Logger log = LoggerFactory.getLogger(PurchaseItemServiceImpl.class);

    private final PurchaseItemRepository purchaseItemRepository;

    private final PurchaseItemMapper purchaseItemMapper;

    private final UserRepository userRepository;

    public PurchaseItemServiceImpl(
        PurchaseItemRepository purchaseItemRepository,
        PurchaseItemMapper purchaseItemMapper,
        UserRepository userRepository
    ) {
        this.purchaseItemRepository = purchaseItemRepository;
        this.purchaseItemMapper = purchaseItemMapper;
        this.userRepository = userRepository;
    }

    @Override
    public PurchaseItemDTO save(PurchaseItemDTO purchaseItemDTO) {
        log.debug("Request to save PurchaseItem : {}", purchaseItemDTO);
        PurchaseItem purchaseItem = purchaseItemMapper.toEntity(purchaseItemDTO);
        var user = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).orElseThrow();
        purchaseItem.setUser(user);
        purchaseItem = purchaseItemRepository.save(purchaseItem);
        return purchaseItemMapper.toDto(purchaseItem);
    }

    @Override
    public Optional<PurchaseItemDTO> partialUpdate(PurchaseItemDTO purchaseItemDTO) {
        log.debug("Request to partially update PurchaseItem : {}", purchaseItemDTO);

        return purchaseItemRepository
            .findById(purchaseItemDTO.getId())
            .map(existingPurchaseItem -> {
                purchaseItemMapper.partialUpdate(existingPurchaseItem, purchaseItemDTO);

                return existingPurchaseItem;
            })
            .map(purchaseItemRepository::save)
            .map(purchaseItemMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PurchaseItemDTO> findAll(Pageable pageable) {
        log.debug("Request to get all PurchaseItems");
        return purchaseItemRepository.findAll(pageable).map(purchaseItemMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<PurchaseItemDTO> findAllByUser() {
        var user = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin().get()).orElseThrow();
        var listOfItems = purchaseItemRepository.findAllByFinishedIsTrueAndUserId(user.getId());
        return purchaseItemMapper.toList(listOfItems);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PurchaseItemDTO> findOne(Long id) {
        log.debug("Request to get PurchaseItem : {}", id);
        return purchaseItemRepository.findById(id).map(purchaseItemMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete PurchaseItem : {}", id);
        purchaseItemRepository.deleteById(id);
    }
}

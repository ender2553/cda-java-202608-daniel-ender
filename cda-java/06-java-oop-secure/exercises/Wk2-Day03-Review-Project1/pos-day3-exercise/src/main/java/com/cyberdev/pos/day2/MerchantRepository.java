package com.cyberdev.pos.day2;

import java.util.Optional;

public interface MerchantRepository {
    Optional<Merchant> findByMerchantId(String merchantId);
}

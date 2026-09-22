package com.cyberdev.pos.day2.inmemory;

import com.cyberdev.pos.day2.Merchant;
import com.cyberdev.pos.day2.MerchantRepository;
import com.cyberdev.pos.exception.DataAccessException;
import com.cyberdev.pos.exception.ValidationException;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GIVEN/PROVIDED IMPLEMENTATION -- not a graded TODO.
 *
 * In-memory stand-in for MerchantRepository, same role as InMemoryTransactionRepository.
 * JdbcMerchantRepository (repository/jdbc) satisfies the same interface against a real
 * database.
 */
public final class InMemoryMerchantRepository implements MerchantRepository {

    private final Map<String, Merchant> byMerchantId = new ConcurrentHashMap<>();

    public void register(Merchant merchant) {
        if (merchant == null) {
            throw new ValidationException("merchant must not be null");
        }
        try {
            byMerchantId.put(merchant.getMerchantId(), merchant);
        } catch (RuntimeException e) {
            throw new DataAccessException("Failed to register merchant " + merchant.getMerchantId(), e);
        }
    }

    @Override
    public Optional<Merchant> findByMerchantId(String merchantId) {
        if (merchantId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(byMerchantId.get(merchantId));
    }
}

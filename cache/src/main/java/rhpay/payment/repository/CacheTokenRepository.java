package rhpay.payment.repository;

import jdk.jfr.Event;
import org.infinispan.Cache;
import rhpay.monitoring.CacheUseEvent;
import rhpay.monitoring.SegmentService;
import rhpay.payment.cache.TokenEntity;
import rhpay.payment.cache.TokenKey;
import rhpay.payment.domain.ShopperId;
import rhpay.payment.domain.Token;
import rhpay.payment.domain.TokenException;
import rhpay.payment.domain.TokenId;
import rhpay.payment.repository.function.FailedTokenFunction;
import rhpay.payment.repository.function.ProcessingTokenFunction;
import rhpay.payment.repository.function.UsedTokenFunction;

public class CacheTokenRepository implements TokenRepository {

    private final Cache<TokenKey, TokenEntity> tokenCache;

    public CacheTokenRepository(Cache<TokenKey, TokenEntity> tokenCache) {
        this.tokenCache = tokenCache;
    }

    @Override
    public void create(Token token) {
        throw new UnsupportedOperationException("token is created on application");
    }

    @Override
    public Token load(ShopperId shopperId, TokenId tokenId) throws TokenException {
        try {
            TokenKey key = new TokenKey(shopperId.value, tokenId.value);

            Event event = new CacheUseEvent(SegmentService.getSegment(tokenCache, key), "loadToken");
            event.begin();

            TokenEntity tokenEntity = tokenCache.get(key);

            event.commit();

            return new Token(shopperId, tokenId, tokenEntity.getStatus().toDomain());
        } catch (Exception e) {
            TokenException exception = new TokenException("Could not load token from cache");
            exception.addSuppressed(e);
            throw exception;
        }
    }

    @Override
    public Token processing(Token token) throws TokenException {
        try {
            TokenKey tokenKey = new TokenKey(token.getShopperId().value, token.getTokenId().value);
            Event event = new CacheUseEvent(SegmentService.getSegment(tokenCache, tokenKey), "changeTokenStatusToProcessing");
            event.begin();
            TokenEntity tokenEntity = tokenCache.compute(tokenKey, new ProcessingTokenFunction());
            event.commit();
            Token newToken = new Token(token.getShopperId(), token.getTokenId(), tokenEntity.getStatus().toDomain());
            return newToken;
        } catch (Exception e) {
            TokenException exception = new TokenException("Could not change the token's status");
            exception.addSuppressed(e);
            throw exception;
        }
    }

    @Override
    public Token used(Token token) throws TokenException {
        try {
            TokenKey tokenKey = new TokenKey(token.getShopperId().value, token.getTokenId().value);
            Event event = new CacheUseEvent(SegmentService.getSegment(tokenCache, tokenKey), "changeTokenStatusToUsed");
            event.begin();
            TokenEntity tokenEntity = tokenCache.compute(tokenKey, new UsedTokenFunction());
            event.commit();
            Token newToken = new Token(token.getShopperId(), token.getTokenId(), tokenEntity.getStatus().toDomain());
            return newToken;
        } catch (Exception e) {
            TokenException exception = new TokenException("Could not change the token's status");
            exception.addSuppressed(e);
            throw exception;
        }

    }

    @Override
    public Token failed(Token token) throws TokenException {
        try {
            TokenKey tokenKey = new TokenKey(token.getShopperId().value, token.getTokenId().value);
            Event event = new CacheUseEvent(SegmentService.getSegment(tokenCache, tokenKey), "changeTokenStatusToFail");
            event.begin();
            TokenEntity tokenEntity = tokenCache.compute(tokenKey, new FailedTokenFunction());
            event.commit();
            Token newToken = new Token(token.getShopperId(), token.getTokenId(), tokenEntity.getStatus().toDomain());
            return newToken;
        } catch (Exception e) {
            TokenException exception = new TokenException("Could not change the token's status");
            exception.addSuppressed(e);
            throw exception;
        }

    }
}

package group.executor.service.handler;

import group.executor.model.ProxyConfigHolder;
import group.executor.service.proxy.validator.ProxyValidator;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

@Service
@PropertySource("classpath:schedule.properties")
public class DefaultProxySourceQueueHandler implements ProxySourceQueueHandler {

    private final Queue<ProxyConfigHolder> proxyQueue = new LinkedBlockingQueue<>();
    private final ProxyValidator proxyValidator;

    public DefaultProxySourceQueueHandler(ProxyValidator proxyValidator) {
        this.proxyValidator = proxyValidator;
    }

    @Override
    public void addProxy(ProxyConfigHolder... proxyConfigHolder) {
        Collections.addAll(proxyQueue, proxyConfigHolder);
    }

    @Override
    public Optional<ProxyConfigHolder> pollProxy() {
        return Optional.ofNullable(proxyQueue.poll());
    }

    @Override
    public Collection<ProxyConfigHolder> pollAllProxy() {
        HashSet<ProxyConfigHolder> result = new HashSet<>(proxyQueue);
        proxyQueue.clear();
        return result;
    }

    @Override
    public boolean isEmpty() {
        return proxyQueue.isEmpty();
    }
    @Override
    @Scheduled(fixedRateString = "${manager.fixedRate}")
    public void removeInvalidProxy() {
        synchronized (this){
            if(!isEmpty()){
                for (ProxyConfigHolder proxyConfigHolder : proxyQueue) {
                    boolean valid = proxyValidator.isValid(proxyConfigHolder);
                    if (!valid){
                        proxyQueue.remove(proxyConfigHolder);
                    }
                }
            }
        }
    }
}

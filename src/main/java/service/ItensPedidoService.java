package service;

import org.springframework.stereotype.Service;
import repository.ItensPedidoRepository;

@Service
public class ItensPedidoService {

    private final ItensPedidoRepository itensPedidoRepository;

    public ItensPedidoService(ItensPedidoRepository itensPedidoRepository) {
        this.itensPedidoRepository = itensPedidoRepository;
    }
}

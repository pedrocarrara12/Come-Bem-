package service;

import org.springframework.stereotype.Service;
import repository.MesaRepository;

@Service
public class MesaService {

    private final MesaRepository mesaRepository;

    public MesaService(MesaRepository mesaRepository) {
        this.mesaRepository = mesaRepository;
    }
}

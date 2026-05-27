package br.com.comebem.entity;

import br.com.comebem.enums.StatusMesa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mesas")
public class Mesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer numero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusMesa status = StatusMesa.LIVRE;

    @Column(nullable = false)
    private Integer capacidade;

    @OneToMany(mappedBy = "mesa")
    private List<Pedido> pedidos = new ArrayList<>();

    protected Mesa() {
    }

    public Mesa(Integer numero, Integer capacidade, StatusMesa status) {
        this.numero = numero;
        this.capacidade = capacidade;
        this.status = status == null ? StatusMesa.LIVRE : status;
    }

    public Long getId() {
        return id;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public StatusMesa getStatus() {
        return status;
    }

    public void setStatus(StatusMesa status) {
        this.status = status;
    }

    public Integer getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(Integer capacidade) {
        this.capacidade = capacidade;
    }
}

package entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "funcionario")
public class Funcionario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false,unique = true)
    private String cpf;

    @OneToMany(mappedBy = "funcionario",cascade = CascadeType.ALL)
    private List<Pedido> pedidos = new ArrayList<>();

    public Funcionario() {
    }

    public Funcionario(String nome, String cpf, List<Pedido> pedidos) {
        this.nome = nome;
        this.cpf = cpf;
        this.pedidos = pedidos;
    }
}

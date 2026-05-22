package entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mesa")
public class Mesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int lugares;

    @OneToMany(mappedBy = "mesa", fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<Pedido> pedidos = new ArrayList<>();

}

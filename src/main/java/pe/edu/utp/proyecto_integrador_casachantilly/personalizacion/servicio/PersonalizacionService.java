package pe.edu.utp.proyecto_integrador_casachantilly.personalizacion.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.ResourceNotFoundException;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.entidad.DetallePedido;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.entidad.Pedido;
import pe.edu.utp.proyecto_integrador_casachantilly.pedido.repositorio.PedidoRepository;
import pe.edu.utp.proyecto_integrador_casachantilly.personalizacion.dto.PersonalizacionDTO;
import pe.edu.utp.proyecto_integrador_casachantilly.personalizacion.dto.PersonalizacionRequest;
import pe.edu.utp.proyecto_integrador_casachantilly.personalizacion.entidad.Personalizacion;
import pe.edu.utp.proyecto_integrador_casachantilly.personalizacion.repositorio.PersonalizacionRepository;

import java.util.List;

@Service
public class PersonalizacionService {

    @Autowired private PersonalizacionRepository personalizacionRepository;
    @Autowired private PedidoRepository pedidoRepository;

    @Transactional
    public PersonalizacionDTO guardar(Integer pedidoId, Integer detalleId,
                                       Integer usuarioId, PersonalizacionRequest req) {
        Pedido pedido = pedidoRepository.findByIdAndUsuarioId(pedidoId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        DetallePedido detalle = pedido.getDetalles().stream()
                .filter(d -> d.getId().equals(detalleId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Detalle de pedido no encontrado"));

        Personalizacion p = personalizacionRepository.findByDetallePedidoId(detalle.getId())
                .orElseGet(() -> {
                    Personalizacion nueva = new Personalizacion();
                    nueva.setDetallePedidoId(detalle.getId());
                    return nueva;
                });

        if (req.saborBizcocho() != null) p.setSaborBizcocho(req.saborBizcocho().trim());
        if (req.tipoRelleno() != null) p.setTipoRelleno(req.tipoRelleno().trim());
        if (req.colorDecorado() != null) p.setColorDecorado(req.colorDecorado().trim());
        if (req.textoPastel() != null) p.setTextoPastel(req.textoPastel().trim());
        if (req.notasCliente() != null) p.setNotasCliente(req.notasCliente().trim());
        if (req.imagenReferenciaUrl() != null) p.setImagenReferenciaUrl(req.imagenReferenciaUrl().trim());

        personalizacionRepository.save(p);
        return toDto(p);
    }

    @Transactional(readOnly = true)
    public List<PersonalizacionDTO> listarPorPedido(Integer pedidoId, Integer usuarioId) {
        Pedido pedido = pedidoRepository.findByIdAndUsuarioId(pedidoId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        List<Integer> detalleIds = pedido.getDetalles().stream().map(DetallePedido::getId).toList();
        return personalizacionRepository.findByDetallePedidoIdIn(detalleIds)
                .stream().map(this::toDto).toList();
    }

    private PersonalizacionDTO toDto(Personalizacion p) {
        return new PersonalizacionDTO(p.getId(), p.getDetallePedidoId(),
                p.getSaborBizcocho(), p.getTipoRelleno(), p.getColorDecorado(),
                p.getTextoPastel(), p.getNotasCliente(), p.getImagenReferenciaUrl());
    }
}

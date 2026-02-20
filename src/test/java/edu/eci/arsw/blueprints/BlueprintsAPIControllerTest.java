package edu.eci.arsw.blueprints;

import edu.eci.arsw.blueprints.controllers.BlueprintsAPIController;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlueprintsAPIControllerTest {

    @Mock
    BlueprintsServices services;

    @InjectMocks
    BlueprintsAPIController controller;

    @Test
    void shouldGetAllBlueprints() {

        Blueprint bp = new Blueprint("john", "house", List.of(new Point(0, 0)));
        when(services.getAllBlueprints()).thenReturn(Set.of(bp));

        ResponseEntity<Set<Blueprint>> res = controller.getAll();

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertNotNull(res.getBody());
        assertEquals(1, res.getBody().size());
    }

    @Test
    void shouldReturnEmptySetWhenNoBlueprints() {
        when(services.getAllBlueprints()).thenReturn(Set.of());

        ResponseEntity<Set<Blueprint>> res = controller.getAll();

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertTrue(res.getBody().isEmpty());
    }


    @Test
    void shouldGetBlueprintsByAuthor() throws BlueprintNotFoundException {
        Blueprint bp = new Blueprint("john", "house", List.of(new Point(1, 1)));
        when(services.getBlueprintsByAuthor("john")).thenReturn(Set.of(bp));

        ResponseEntity<?> res = controller.byAuthor("john");

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertNotNull(res.getBody());
    }

    @Test
    void shouldReturn404WhenAuthorNotFound() throws BlueprintNotFoundException {
        when(services.getBlueprintsByAuthor("unknown"))
                .thenThrow(new BlueprintNotFoundException("No blueprints for author: unknown"));

        ResponseEntity<?> res = controller.byAuthor("unknown");

        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
    }

    @Test
    void shouldGetBlueprintByAuthorAndName() throws BlueprintNotFoundException {
        Blueprint bp = new Blueprint("john", "house", List.of(new Point(0, 0)));
        when(services.getBlueprint("john", "house")).thenReturn(bp);

        ResponseEntity<?> res = controller.byAuthorAndName("john", "house");

        assertEquals(HttpStatus.OK, res.getStatusCode());
        Blueprint body = (Blueprint) res.getBody();
        assertEquals("john", body.getAuthor());
        assertEquals("house", body.getName());
    }

    @Test
    void shouldReturn404WhenBlueprintNotFound() throws BlueprintNotFoundException {
        when(services.getBlueprint("john", "unknown"))
                .thenThrow(new BlueprintNotFoundException("Blueprint not found: john/unknown"));

        ResponseEntity<?> res = controller.byAuthorAndName("john", "unknown");

        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
    }


    @Test
    void shouldCreateBlueprint() throws BlueprintPersistenceException {
        BlueprintsAPIController.NewBlueprintRequest req =
                new BlueprintsAPIController.NewBlueprintRequest("john", "garage", List.of(new Point(1, 1)));
        doNothing().when(services).addNewBlueprint(any(Blueprint.class));

        ResponseEntity<?> res = controller.add(req);

        assertEquals(HttpStatus.CREATED, res.getStatusCode());
        verify(services, times(1)).addNewBlueprint(any(Blueprint.class));
    }

    @Test
    void shouldReturn403WhenBlueprintAlreadyExists() throws BlueprintPersistenceException {
        BlueprintsAPIController.NewBlueprintRequest req =
                new BlueprintsAPIController.NewBlueprintRequest("john", "house", List.of(new Point(1, 1)));
        doThrow(new BlueprintPersistenceException("Blueprint already exists"))
                .when(services).addNewBlueprint(any(Blueprint.class));

        ResponseEntity<?> res = controller.add(req);

        assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
    }


    @Test
    void shouldAddPointToBlueprint() throws BlueprintNotFoundException {
        doNothing().when(services).addPoint("john", "house", 5, 5);

        ResponseEntity<?> res = controller.addPoint("john", "house", new Point(5, 5));

        assertEquals(HttpStatus.ACCEPTED, res.getStatusCode());
        verify(services, times(1)).addPoint("john", "house", 5, 5);
    }

    @Test
    void shouldReturn404WhenAddingPointToBlueprintNotFound() throws BlueprintNotFoundException {
        doThrow(new BlueprintNotFoundException("Blueprint not found"))
                .when(services).addPoint("john", "unknown", 5, 5);

        ResponseEntity<?> res = controller.addPoint("john", "unknown", new Point(5, 5));

        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
    }
}
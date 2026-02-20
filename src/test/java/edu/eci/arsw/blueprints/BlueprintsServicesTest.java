package edu.eci.arsw.blueprints;

import edu.eci.arsw.blueprints.filters.BlueprintsFilter;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistence;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlueprintsServicesTest {

    @Mock
    BlueprintPersistence persistence;

    @Mock
    BlueprintsFilter filter;

    @InjectMocks
    BlueprintsServices services;

    @Test
    void shouldAddNewBlueprint() throws BlueprintPersistenceException {
        Blueprint bp = new Blueprint("john", "house", List.of(new Point(0, 0)));
        doNothing().when(persistence).saveBlueprint(bp);

        services.addNewBlueprint(bp);

        verify(persistence, times(1)).saveBlueprint(bp);
    }

    @Test
    void shouldNotAddDuplicateBlueprint() throws BlueprintPersistenceException {

        Blueprint bp = new Blueprint("john", "house", List.of(new Point(0, 0)));
        doThrow(new BlueprintPersistenceException("Blueprint already exists"))
                .when(persistence).saveBlueprint(bp);

        assertThrows(BlueprintPersistenceException.class, () -> services.addNewBlueprint(bp));
    }


    @Test
    void shouldGetAllBlueprints() {
        Blueprint bp1 = new Blueprint("john", "house", List.of(new Point(0, 0)));
        Blueprint bp2 = new Blueprint("jane", "garden", List.of(new Point(1, 1)));
        when(persistence.getAllBlueprints()).thenReturn(Set.of(bp1, bp2));

        Set<Blueprint> result = services.getAllBlueprints();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void shouldReturnEmptySetWhenNoBlueprints() {
        when(persistence.getAllBlueprints()).thenReturn(Set.of());

        Set<Blueprint> result = services.getAllBlueprints();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test
    void shouldGetBlueprintsByAuthor() throws BlueprintNotFoundException {
        Blueprint bp = new Blueprint("john", "house", List.of(new Point(0, 0)));
        when(persistence.getBlueprintsByAuthor("john")).thenReturn(Set.of(bp));

        Set<Blueprint> result = services.getBlueprintsByAuthor("john");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("john", result.iterator().next().getAuthor());
    }

    @Test
    void shouldNotGetBlueprintsForUnknownAuthor() throws BlueprintNotFoundException {

        when(persistence.getBlueprintsByAuthor("unknown"))
                .thenThrow(new BlueprintNotFoundException("No blueprints for author: unknown"));

        assertThrows(BlueprintNotFoundException.class, () -> services.getBlueprintsByAuthor("unknown"));
    }

    @Test
    void shouldGetBlueprintWithFilterApplied() throws BlueprintNotFoundException {

        Blueprint raw = new Blueprint("john", "house",
                List.of(new Point(1, 1), new Point(1, 1), new Point(2, 2)));
        Blueprint filtered = new Blueprint("john", "house",
                List.of(new Point(1, 1), new Point(2, 2)));
        when(persistence.getBlueprint("john", "house")).thenReturn(raw);
        when(filter.apply(raw)).thenReturn(filtered);

        Blueprint result = services.getBlueprint("john", "house");

        assertNotNull(result);
        assertEquals(2, result.getPoints().size());
        verify(filter, times(1)).apply(raw);
    }

    @Test
    void shouldNotGetBlueprintWhenNotFound() throws BlueprintNotFoundException {

        when(persistence.getBlueprint("john", "unknown"))
                .thenThrow(new BlueprintNotFoundException("Blueprint not found: john/unknown"));

        assertThrows(BlueprintNotFoundException.class, () -> services.getBlueprint("john", "unknown"));
    }


    @Test
    void shouldAddPointToBlueprint() throws BlueprintNotFoundException {
        doNothing().when(persistence).addPoint("john", "house", 5, 5);

        services.addPoint("john", "house", 5, 5);

        verify(persistence, times(1)).addPoint("john", "house", 5, 5);
    }

    @Test
    void shouldNotAddPointWhenBlueprintNotFound() throws BlueprintNotFoundException {
        doThrow(new BlueprintNotFoundException("Blueprint not found"))
                .when(persistence).addPoint("john", "unknown", 5, 5);

        assertThrows(BlueprintNotFoundException.class, () -> services.addPoint("john", "unknown", 5, 5));
    }
}

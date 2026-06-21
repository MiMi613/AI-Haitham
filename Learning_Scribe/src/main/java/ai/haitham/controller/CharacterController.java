package ai.haitham.controller;

import ai.haitham.service.CharacterService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/characters")
public class CharacterController {

    private final CharacterService characterService;

    public CharacterController(CharacterService characterService) {
        this.characterService = characterService;
    }

    @GetMapping
    public List<Map<String, String>> listCharacters() {
        return characterService.getAll();
    }
}

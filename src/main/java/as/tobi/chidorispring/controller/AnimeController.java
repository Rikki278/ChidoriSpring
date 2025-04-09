package as.tobi.chidorispring.controller;

import as.tobi.chidorispring.service.KitsuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AnimeController {

    private final KitsuService kitsuService;

    @Autowired
    public AnimeController(KitsuService kitsuService) {
        this.kitsuService = kitsuService;
    }

    @GetMapping("/anime/titles")
    public List<String> getAllAnimeTitles() {
        return kitsuService.getAnimeTitles();
    }
}

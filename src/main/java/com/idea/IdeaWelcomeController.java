package com.idea;



import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IdeaWelcomeController {
	
		@GetMapping("/index")
		public String welcome() {
			return "index";
		}
}



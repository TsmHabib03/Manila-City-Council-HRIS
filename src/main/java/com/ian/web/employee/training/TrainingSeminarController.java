package com.ian.web.employee.training;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ian.web.common.model.UXMessage;

import lombok.RequiredArgsConstructor;

/**
 * Training and Seminar under Employee Management (CR Request ID 015).
 * Routes are staff-only via SecurityConfig.ARCHIVE_ADMIN.
 */
@Controller
@RequiredArgsConstructor
public class TrainingSeminarController {

	private final TrainingSeminarRepository trainingSeminarRepository;

	@GetMapping("/trainings")
	public String viewTrainings(Model model) {
		model.addAttribute("trainingList", trainingSeminarRepository.findAllByOrderByTrainingDateDesc());
		model.addAttribute("training", new TrainingSeminar());
		return "employee/training/training-list";
	}

	@PostMapping("/saveTraining")
	public String saveTraining(TrainingSeminar training, final RedirectAttributes redirect) {
		trainingSeminarRepository.save(training);
		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Saved."));
		return "redirect:/trainings";
	}

	@GetMapping("/deleteTraining/{id}")
	public String deleteTraining(@PathVariable long id, final RedirectAttributes redirect) {
		trainingSeminarRepository.deleteById(id);
		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Deleted."));
		return "redirect:/trainings";
	}
}

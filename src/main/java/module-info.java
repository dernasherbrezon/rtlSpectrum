module rtlspectrum {
	requires javafx.controls;
	requires javafx.fxml;

	opens ru.r2cloud.rtlspectrum to javafx.graphics,javafx.fxml;
	
}
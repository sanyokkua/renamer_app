package ua.renamer.app.core.commands.preparation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import ua.renamer.app.core.abstracts.AppFileCommand;
import ua.renamer.app.core.enums.TextCaseOptions;
import ua.renamer.app.core.model.AppFile;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeCasePreparePrepareCommand extends AppFileCommand {
    @Builder.Default
    private TextCaseOptions textCase = TextCaseOptions.CAMEL_CASE;
    @Builder.Default
    private boolean capitalize = false;

    @Override
    public AppFile processItem(AppFile item) {
        return null;
    }
}

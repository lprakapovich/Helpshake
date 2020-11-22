package com.application.helpshake.helper;

import androidx.fragment.app.FragmentManager;

import com.application.helpshake.ui.DialogMessage;

public class DialogBuilder {
    public static void showMessageDialog(FragmentManager manager,
                                       String title,
                                       String message) {

        DialogMessage dialog = new DialogMessage(title, message);
        dialog.show(manager, "error");
    }
}


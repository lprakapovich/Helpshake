package com.application.helpshake.util;

import androidx.fragment.app.FragmentManager;

import com.application.helpshake.dialog.DialogMessage;

public class DialogBuilder {
    public static void showMessageDialog(FragmentManager manager,
                                       String title,
                                       String message) {

        DialogMessage dialog = new DialogMessage(title, message);
        dialog.show(manager, "error");
    }
}


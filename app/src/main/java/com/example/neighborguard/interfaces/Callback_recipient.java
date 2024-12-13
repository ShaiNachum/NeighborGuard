package com.example.neighborguard.interfaces;

import com.example.neighborguard.model.ExtendedUser;

public interface Callback_recipient {
    void recipientClicked(double lat, double lng);
    void recipientPicked(ExtendedUser extendedUser, int position);
    void recipientCanceled(ExtendedUser extendedUser, int position);
}

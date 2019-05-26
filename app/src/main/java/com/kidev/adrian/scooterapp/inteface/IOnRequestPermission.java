package com.kidev.adrian.scooterapp.inteface;

public interface IOnRequestPermission {
    public void onPermissionAccepted(String permiso);
    public void onPermissionDenied(String permiso);
}

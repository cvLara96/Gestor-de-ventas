package com.example.ventasandusers;

import java.util.Objects;

public class Usuario {

    private String nombre, contraseña, permiso;

    public Usuario() {
    }

    public Usuario(String nombre, String contraseña, String permiso) {
        this.nombre = nombre;
        this.contraseña = contraseña;
        this.permiso = permiso;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }

    public String getPermiso() {
        return permiso;
    }

    public void setPermiso(String permiso) {
        this.permiso = permiso;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(nombre, usuario.nombre) && Objects.equals(contraseña, usuario.contraseña) && Objects.equals(permiso, usuario.permiso);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre, contraseña, permiso);
    }
}

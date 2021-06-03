{ pkgs ? import <nixpkgs> { }, lib ? pkgs.stdenv.lib }:

pkgs.mkShell rec {
  name = "quil-env";
  buildInputs = with pkgs; [
    clojure
    xorg_sys_opengl
  ];
  LD_LIBRARY_PATH = "${lib.makeLibraryPath buildInputs}";

  # we need to make sure the library is on the path for JOGL;
  # also, there's a bug that is avoided with the second config line
  # https://github.com/processing/processing/issues/5476
  JAVA_OPTS = "-Djava.library.path=${lib.makeLibraryPath buildInputs}"; 
  LIBGL_ALWAYS_SOFTWARE = true;
}

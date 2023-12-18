resource "null_resource" "deploy" {
  # Depends on the existing GCP instance
  depends_on = [google_compute_instance.vm_instance]
  provisioner "remote-exec" {
    inline = [
      "ls /tmp/ && java -version"
    ]
    connection {
      type        = "ssh"
      user        = "logicfocus_devops"
      private_key = file("/home/logicfocus/.ssh/id_rsa")
      host        = google_compute_instance.vm_instance.network_interface.0.access_config.0.nat_ip
    }
  }

  provisioner "remote-exec" {
    inline = [
      "ls /tmp/ && java -version && nohup java -jar /tmp/gradleSampleArt.jar > /tmp/result.log &"
    ]
    connection {
      type        = "ssh"
      user        = "logicfocus_devops"
      private_key = file("/home/logicfocus/.ssh/id_rsa")
      host        = google_compute_instance.vm_instance.network_interface.0.access_config.0.nat_ip
    }
  }
}

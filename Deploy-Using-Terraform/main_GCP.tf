provider "google" {
  project = "lftest-407308"
  credentials = file("/home/logicfocus/terraform/gcpcred.json")
  region  = "us-east1"
  zone    = "us-east1-b"
}

resource "google_compute_instance" "vm_instance" {
  name         = "lf-instancetest"
  machine_type = "e2-medium"

  tags = ["cdropublicegr", "cdropublicing", "cloudbeescdro", "default-allow-https", "vmpub", "cicd-test-egress", "cicd-test-rule"]

  boot_disk {
    initialize_params {
      image = "debian-11-bullseye-v20231115"
    }
  }
  network_interface {
    network = "default"
    access_config {}
  }

  metadata = {
    ssh-keys = "logicfocus_devops:${file("/home/logicfocus/.ssh/id_rsa.pub")}"
  }

  provisioner "remote-exec" {
    inline = [
      "wget -O - https://apt.corretto.aws/corretto.key | sudo gpg --dearmor -o /usr/share/keyrings/corretto-keyring.gpg && echo \"deb [signed-by=/usr/share/keyrings/corretto-keyring.gpg] https://apt.corretto.aws stable main\" | sudo tee /etc/apt/sources.list.d/corretto.list",
      "sudo apt-get update; sudo apt-get install -y java-1.8.0-amazon-corretto-jdk -y",
    ]
    connection {
      type        = "ssh"
      user        = "logicfocus_devops"
      private_key = file("/home/logicfocus/.ssh/id_rsa")
      host        = self.network_interface[0].access_config[0].nat_ip
    }
  }

  provisioner "file" {
    source      = "/home/logicfocus/jai/cicdtest/build/libs/gradleSampleArt.jar"
    destination = "/tmp/gradleSampleArt.jar"
    connection {
      type        = "ssh"
      user        = "logicfocus_devops"
      private_key = file("/home/logicfocus/.ssh/id_rsa")
      host        = self.network_interface[0].access_config[0].nat_ip
    }
  }
  
}
output "ip" {
  value = "${google_compute_instance.vm_instance.network_interface.0.access_config.0.nat_ip}"
}
